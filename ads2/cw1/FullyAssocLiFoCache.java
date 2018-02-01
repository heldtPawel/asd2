package ads2.cw1;

import java.util.Stack;
import java.util.HashMap;
import java.util.Set;

class FullyAssocLiFoCache implements Cache {

    final private static boolean VERBOSE = false;

    final private int CACHE_SZ;
    final private int CACHELINE_SZ;
    final private int CL_MASK;
    final private int CL_SHIFT;

    // WV: because the cache replacement policy is "Last In First Out" you only need to know the "Last Used" location
    // "Last Used" means accessed for either read or write
    // The helper functions below contain all needed assignments to last_used_loc so I recommend you use these.

    private int last_used_loc;
    // WV: Your other data structures here
    // Hint: You need 4 data structures
    // - One for the cache storage
    private int cache_storage;    
    // - One to manage locations in the cache
    private int location_stack;    
    // And because the cache is Fully Associative:
    // - One to translate between memory addresses and cache locations
    private int address_to_cache_loc;
    // - One to translate between cache locations and memory addresses  
    private int cache_loc_to_address;
    


    FullyAssocLiFoCache(int cacheSize, int cacheLineSize) {

        CACHE_SZ =  cacheSize;
        CACHELINE_SZ = cacheLineSize;
        CL_MASK = CACHELINE_SZ - 1;
        Double cls = Math.log(CACHELINE_SZ)/Math.log(2);
        CL_SHIFT = cls.intValue();

        last_used_loc = CACHE_SZ/CACHELINE_SZ - 1;
        // WV: Your initialisations here
       
    }

    public void flush(int[] ram, Status status) {
        if (VERBOSE) System.out.println("Flushing cache");
        // WV: Your other data structures here

        status.setFlushed(true);
    }

    public int read(int address,int[] ram,Status status) {
        return read_data_from_cache( ram, address, status);
    }

    public void write(int address,int data, int[] ram,Status status) {
        write_data_to_cache(ram, address, data, status);
    }

    private void write_data_to_cache(int[] ram, int address, int data, Status status){
        status.setReadWrite(false); // i.e. a write
        status.setAddress(address);
        status.setData(data);
        status.setEvicted(false);
        // Your code here
        // The cache policy is write-back, so the writes are always to the cache. 
        // The update policy is write allocate: on a write miss, a cache line is loaded to cache, followed by a write operation. 
         // ...

    }

    private int read_data_from_cache(int[] ram,int address, Status status){
        status.setReadWrite(true); // i.e. a read
        status.setAddress(address);
        status.setEvicted(false);
        status.setHitOrMiss(true); // i.e. a hit
        // Your code here
        // Reads are always to the cache. On a read miss you need to fetch a cache line from the DRAM
        // If the data is not yet in the cache (read miss),fetch it from the DRAM
        // Get the data from the cache
         // ...

        status.setData(data);
        return data;
    }

    // You might want to use the following methods as helpers
    
    // On read miss, fetch a cache line    
    private void read_from_mem_on_miss(int[] ram,int address){
        int[] cache_line = new int[CACHELINE_SZ];
        int loc;
        // Your code here
         // ...

        last_used_loc=loc;
   }

    // On write, modify a cache line
    private void update_cache_entry(int address, int data){
        int loc;
         // Your code here
         // ...

        last_used_loc=loc;
       }

    // When we fetch a cache entry, we also update the last used location
    private int fetch_cache_entry(int address){
        int[] cache_line;
        int loc;
         // Your code here
         // ...
        last_used_loc=loc;
        return cache_line[cache_line_address(address)];
    }

    private int get_next_free_location(){
         // Your code here
         // ...
        
    }

    private void evict_location(int loc){
         // Your code here
         // ...
        
    }

    private boolean cache_is_full(){
         // Your code here
         // ...
        
    }

    private void write_to_mem_on_evict(int[] ram, int loc){

        int evicted_cl_address;
        int[] cache_line;
        if (VERBOSE) System.out.println("Cache line to RAM: ");
        // Your code here
         // ...
        

        evict_location(loc);
    }

    private boolean address_in_cache_line(int address) {
        // Your code here
         // ...
        
    }

    private int cache_line_address(int address) {
        return address>>CL_SHIFT;
    }

    private int cache_entry_position(int cl_address) {
        return cl_address & CL_MASK;
    }

    private int mem_address(int cl_address) {
        return cl_address<<CL_SHIFT;
    }

}
