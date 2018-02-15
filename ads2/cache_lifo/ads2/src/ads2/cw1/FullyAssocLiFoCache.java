package ads2.cw1;

/**
 * Created by wim on 28/11/2017.
 * The public interface of this class is provided by Cache
 * All other methods are private. 
 * You must implement/complete all these methods
 * You are allow to create helper methods to do this, put them at the end of the class 
 */
import ads2.cw1.Cache;

import java.util.Stack;
import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class FullyAssocLiFoCache implements Cache {

	final private static boolean VERBOSE = false;

	final private int CACHE_SZ;
	final private int CACHELINE_SZ;
	final private int CL_MASK;
	final private int CL_SHIFT;

	// WV: because the cache replacement policy is "Last In First Out" you only need
	// to know the "Last Used" location
	// "Last Used" means accessed for either read or write
	// The helper functions below contain all needed assignments to last_used_loc so
	// I recommend you use these.
	int r = 0;
	private int last_used_loc;
	// WV: Your other data structures here
	// Hint: You need 4 data structures
	// - One for the cache storage
	private int[] cache_storage;
	// - One to manage locations in the cache
	private Stack<Integer> location_stack;
	// And because the cache is Fully Associative:
	// - One to translate between memory addresses and cache locations
	private HashMap<Integer, Integer> address_to_cache_loc;
	// - One to translate between cache locations and memory addresses
	private HashMap<Integer, Integer> cache_loc_to_address;

	FullyAssocLiFoCache(int cacheSize, int cacheLineSize) {
		
		CACHE_SZ = cacheSize;
		CACHELINE_SZ = cacheLineSize;
		CL_MASK = CACHELINE_SZ - 1;
		Double cls = Math.log(CACHELINE_SZ) / Math.log(2);
		CL_SHIFT = cls.intValue();

		last_used_loc = CACHE_SZ / CACHELINE_SZ - 1;
		// WV: Your initialisations here
		cache_storage = new int[CACHE_SZ];
		location_stack = new Stack<Integer>();
		address_to_cache_loc = new HashMap<Integer, Integer>();
		cache_loc_to_address = new HashMap<Integer, Integer>();
		
		//filling location_stack with empty cachelines
		for (int i = 0; i < CACHE_SZ / CACHELINE_SZ; i++) {
			location_stack.push(i);
		}

	}

	public void flush(int[] ram, Status status) {
		if (VERBOSE)
			System.out.println("Flushing cache");
		// WV: Your other data structures here
		//System.out.println(address_to_cache_loc.toString());
		
		//flushing cache and saving the values to memory
		for (int i = 0; i < CACHE_SZ / CACHELINE_SZ; i++) {
			if (address_to_cache_loc.containsKey(i)) {
				write_to_mem_on_evict(ram, i);
			}
		}
		
		address_to_cache_loc.clear();
		cache_loc_to_address.clear();
		
		status.setFreeLocations(location_stack.size());
		status.setFlushed(true);
	}

	public int read(int address, int[] ram, Status status) {

		return read_data_from_cache(ram, address, status);
	}

	public void write(int address, int data, int[] ram, Status status) {

		write_data_to_cache(ram, address, data, status);
	}

	// The next two methods are the most important ones as they implement read() and
	// write()
	// Both methods modify the status object that is provided as argument

	private void write_data_to_cache(int[] ram, int address, int data, Status status) {
		status.setReadWrite(false); // i.e. a write
		status.setAddress(address);
		status.setData(data);
		status.setEvicted(false);
		// Your code here
		// The cache policy is write-back, so the writes are always to the cache.
		// The update policy is write allocate: on a write miss, a cache line is loaded
		// to cache, followed by a write operation.
		// ...

		if (address_in_cache_line(address)) {
			status.setHitOrMiss(true);
			update_cache_entry(address, data);
		} else {
			status.setHitOrMiss(false);
			
			if (cache_is_full()) {
				status.setEvicted(true);
				status.setEvictedCacheLineAddr(address_to_cache_loc.get(last_used_loc)/ CACHELINE_SZ);
				
				write_to_mem_on_evict(ram, last_used_loc);
			}
			last_used_loc = get_next_free_location();
		
			address_to_cache_loc.put(last_used_loc, cache_line_address(address)*CACHELINE_SZ);
			cache_loc_to_address.put(cache_line_address(address)*CACHELINE_SZ, last_used_loc);
			
			cache_storage[last_used_loc*CACHELINE_SZ + cache_entry_position(address)] = data;
			//for (int i = 0; i < CACHELINE_SZ; i++) {
			//	cache_storage[(last_used_loc * CACHELINE_SZ) + i] = ram[(cache_line_address(address) * CACHELINE_SZ)
			//			+ i];
			//}
			status.setFreeLocations(location_stack.size());
			//System.out.println(location_stack.size());
		}
	}

	private int read_data_from_cache(int[] ram, int address, Status status) {
		status.setReadWrite(true); // i.e. a read
		status.setAddress(address);
		status.setEvicted(false);
		status.setHitOrMiss(true); // i.e. a hit
		// Your code here
		// Reads are always to the cache. On a read miss you need to fetch a cache line
		// from the DRAM
		// If the data is not yet in the cache (read miss),fetch it from the DRAM
		// Get the data from the cache
		// ...

		if (!address_in_cache_line(address)) {
			status.setHitOrMiss(false);

			if (cache_is_full()) {
				status.setEvicted(true);
				status.setEvictedCacheLoc(address_to_cache_loc.get(last_used_loc)/CACHELINE_SZ);

				write_to_mem_on_evict(ram, last_used_loc);
			}
			
			read_from_mem_on_miss(ram, address);
			
		}
		
		int data = fetch_cache_entry(address);
		status.setFreeLocations(location_stack.size());
		status.setData(data);
		return data;
	}

	// You might want to use the following methods as helpers
	// but it is not mandatory, you may write your own as well

	// On read miss, fetch a cache line
	private void read_from_mem_on_miss(int[] ram, int address) {
		int[] cache_line;
		int loc;
		// Your code here // ...
		
		loc = cache_line_address(address);
		last_used_loc = get_next_free_location();

		for (int i = 0; i < CACHELINE_SZ; i++) {
			cache_storage[last_used_loc*CACHELINE_SZ+i] = ram[loc*CACHELINE_SZ + i];
		}
		address_to_cache_loc.put(last_used_loc, loc * CACHELINE_SZ);
		cache_loc_to_address.put(loc * CACHELINE_SZ, last_used_loc);
	}

	// On write, modify a cache line
	private void update_cache_entry(int address, int data) {
		int loc;
		// Your code here
		// ...
		//System.out.println(((cache_loc_to_address.get(cache_line_address(address)*CACHELINE_SZ) * CACHELINE_SZ) + cache_entry_position(address)));
		loc = ((cache_loc_to_address.get(cache_line_address(address)*CACHELINE_SZ) * CACHELINE_SZ) + cache_entry_position(address));
		cache_storage[loc] = data;
		//System.out.println(cache_loc_to_address.get(cache_line_address(address)*CACHELINE_SZ));
		last_used_loc = cache_loc_to_address.get(cache_line_address(address)*CACHELINE_SZ);
		
	}

	// When we fetch a cache entry, we also update the last used location
	private int fetch_cache_entry(int address) {
		int[] cache_line;
		int loc;
		// Your code here
		// ...
		// initializing variables
		loc = cache_loc_to_address.get(cache_line_address(address)*CACHELINE_SZ);
		
		last_used_loc = loc;
		// cache_line = new int[CACHELINE_SZ];

		return cache_storage[(loc * CACHELINE_SZ) + cache_entry_position(address)];
	}

	// Should return the next free location in the cache
	private int get_next_free_location() {
		// Your code here
		// ...
		// If cache is not yet populated free location returns the cache address of next
		// free cache line

		return location_stack.pop();

	}

	// Given a cache location, evict the cache line stored there
	private void evict_location(int loc) {
		// Your code here
		// ...
		//System.out.println(cache_loc_to_address.get(address_to_cache_loc.get(loc)));
		cache_loc_to_address.remove(address_to_cache_loc.get(loc));
		address_to_cache_loc.remove(loc);
		location_stack.push(loc);
	}

	private boolean cache_is_full() {
		// Your code here
		// ...
		return location_stack.isEmpty();
	}

	// When evicting a cache line, write its contents back to main memory
	private void write_to_mem_on_evict(int[] ram, int loc) {
		
		int evicted_cl_address;
		int[] cache_line;
		if (VERBOSE)
			System.out.println("Cache line to RAM: ");
		// Your code here
		// ...
		// cache_line = new int[CACHELINE_SZ];
		evicted_cl_address = loc * CACHELINE_SZ;
		
		for (int i = 0; i < CACHELINE_SZ; i++) {
			ram[address_to_cache_loc.get(loc) + i] = cache_storage[evicted_cl_address + i];
		}

		evict_location(loc);
	}

	// Test if a main memory address is in a cache line stored in the cache
	// In other words, is the value for this memory address stored in the cache?
	private boolean address_in_cache_line(int address) {
		// Your code here
		// ...
		//System.out.println(cache_loc_to_address.get(cache_line_address(address)*CACHELINE_SZ));
		return cache_loc_to_address.containsKey(cache_line_address(address)*CACHELINE_SZ);
	}

	// Given a main memory address, return the corresponding cache line address
	private int cache_line_address(int address) {
		return address >> CL_SHIFT;
	}

	// Given a main memory address, return the corresponding index into the cache
	// line
	private int cache_entry_position(int address) {
		return address & CL_MASK;
	}

	// Given a cache line address, return the corresponding main memory address
	// This is the starting address of the cache line in main memory
	private int cache_line_start_mem_address(int cl_address) {
		return cl_address << CL_SHIFT;
	}

}
