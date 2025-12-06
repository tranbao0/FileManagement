package Storage;

import java.util.Arrays;

// simulates a fixed-size virtual disk with block-based storage
// manages raw byte storage, block allocation, and deallocation
// this class is independent of files and directories
public class Storage {
    // storage configuration
    private final int BLOCK_SIZE;      // size of each block in bytes
    private final int NUM_BLOCKS;      // total number of blocks
    private final int TOTAL_SIZE;      // total storage size in bytes

    // storage data structures
    private final byte[] disk;         // the virtual disk (raw storage)
    private final boolean[] freeBlocks; // true = free, false = used

    // statistics
    private int usedBlockCount;

    // constructors -----------------------------------------

    // creates a new storage with specified block size and number of blocks
    // @param blockSize size of each block in bytes (e.g., 512)
    // @param numBlocks total number of blocks (e.g., 2048)
    public Storage(int blockSize, int numBlocks) {
        this.BLOCK_SIZE = blockSize;
        this.NUM_BLOCKS = numBlocks;
        this.TOTAL_SIZE = blockSize * numBlocks;

        this.disk = new byte[TOTAL_SIZE];
        this.freeBlocks = new boolean[NUM_BLOCKS];

        // initialize all blocks as free
        Arrays.fill(freeBlocks, true);
        this.usedBlockCount = 0;
    }

    // default constructor: 512-byte blocks, 2048 blocks (1 MB total)
    public Storage() {
        this(512, 2048);
    }

    // block allocation -------------------------------------

    // allocates a single free block
    // @return the block ID of the allocated block, or -1 if no free blocks
    public int allocateBlock() {
        for (int i = 0; i < NUM_BLOCKS; i++) {
            if (freeBlocks[i]) {
                freeBlocks[i] = false;
                usedBlockCount++;
                return i;
            }
        }
        return -1; // no free blocks available
    }

    // allocates multiple free blocks (not guaranteed contiguous)
    // @param count number of blocks to allocate
    // @return array of allocated block IDs, or null if not enough free blocks
    public int[] allocateBlocks(int count) {
        if (count <= 0 || count > getFreeBlockCount()) {
            return null;
        }

        int[] blockIds = new int[count];
        int allocated = 0;

        for (int i = 0; i < NUM_BLOCKS && allocated < count; i++) {
            if (freeBlocks[i]) {
                freeBlocks[i] = false;
                blockIds[allocated] = i;
                allocated++;
                usedBlockCount++;
            }
        }

        return blockIds;
    }

    // deallocates (frees) a single block
    // @param blockId the block ID to free
    // @return true if successful, false if invalid or already free
    public boolean deallocateBlock(int blockId) {
        if (!isValidBlockId(blockId)) {
            return false;
        }

        if (freeBlocks[blockId]) {
            return false; // already free
        }

        freeBlocks[blockId] = true;
        usedBlockCount--;

        // optionally clear the block data
        clearBlock(blockId);

        return true;
    }

    // deallocates multiple blocks
    // @param blockIds array of block IDs to free
    public void deallocateBlocks(int[] blockIds) {
        if (blockIds == null) {
            return;
        }

        for (int blockId : blockIds) {
            deallocateBlock(blockId);
        }
    }

    // data operations --------------------------------------

    // writes data to a specific block
    // if data is larger than block size, only BLOCK_SIZE bytes are written
    // @return true if successful, false if block ID is invalid
    public boolean write(int blockId, byte[] data) {
        if (!isValidBlockId(blockId) || data == null) {
            return false;
        }

        int startPosition = blockId * BLOCK_SIZE;
        int bytesToWrite = Math.min(data.length, BLOCK_SIZE);

        System.arraycopy(data, 0, disk, startPosition, bytesToWrite);

        return true;
    }

    // reads data from a specific block
    // @return byte array containing the block data, or null if invalid block ID
    public byte[] read(int blockId) {
        if (!isValidBlockId(blockId)) {
            return null;
        }

        int startPosition = blockId * BLOCK_SIZE;
        byte[] data = new byte[BLOCK_SIZE];

        System.arraycopy(disk, startPosition, data, 0, BLOCK_SIZE);

        return data;
    }

    // writes data at a specific offset within a block
    // @param blockId the block ID
    // @param offset offset within the block (0 to BLOCK_SIZE-1)
    // @param data data to write
    // @return true if successful, false otherwise
    public boolean writeBytes(int blockId, int offset, byte[] data) {
        if (!isValidBlockId(blockId) || data == null || offset < 0 || offset >= BLOCK_SIZE) {
            return false;
        }

        int startPosition = blockId * BLOCK_SIZE + offset;
        int bytesToWrite = Math.min(data.length, BLOCK_SIZE - offset);

        System.arraycopy(data, 0, disk, startPosition, bytesToWrite);

        return true;
    }

    // reads a specific number of bytes from a block starting at an offset
    // @param blockId the block ID
    // @param offset offset within the block
    // @param length number of bytes to read
    // @return byte array with the data, or null if invalid parameters
    public byte[] readBytes(int blockId, int offset, int length) {
        if (!isValidBlockId(blockId) || offset < 0 || offset >= BLOCK_SIZE || length <= 0) {
            return null;
        }

        int startPosition = blockId * BLOCK_SIZE + offset;
        int bytesToRead = Math.min(length, BLOCK_SIZE - offset);
        byte[] data = new byte[bytesToRead];

        System.arraycopy(disk, startPosition, data, 0, bytesToRead);

        return data;
    }

    // writes data across multiple blocks
    // @param blockIds array of block IDs to write to
    // @param data data to write
    // @return true if successful
    public boolean writeMultiBlock(int[] blockIds, byte[] data) {
        if (blockIds == null || data == null) {
            return false;
        }

        int dataOffset = 0;

        for (int blockId : blockIds) {
            if (dataOffset >= data.length) {
                break; // all data written
            }

            int bytesToWrite = Math.min(data.length - dataOffset, BLOCK_SIZE);
            byte[] blockData = Arrays.copyOfRange(data, dataOffset, dataOffset + bytesToWrite);

            write(blockId, blockData);
            dataOffset += bytesToWrite;
        }

        return true;
    }

    // reads data from multiple blocks
    // @param blockIds array of block IDs to read from
    // @return combined data from all blocks
    public byte[] readMultiBlock(int[] blockIds) {
        if (blockIds == null || blockIds.length == 0) {
            return null;
        }

        byte[] result = new byte[blockIds.length * BLOCK_SIZE];
        int offset = 0;

        for (int blockId : blockIds) {
            byte[] blockData = read(blockId);
            if (blockData != null) {
                System.arraycopy(blockData, 0, result, offset, BLOCK_SIZE);
                offset += BLOCK_SIZE;
            }
        }

        return result;
    }

    // query methods ----------------------------------------

    // checks if a block is free
    public boolean isFree(int blockId) {
        if (!isValidBlockId(blockId)) {
            return false;
        }
        return freeBlocks[blockId];
    }

    // returns the number of free blocks
    public int getFreeBlockCount() {
        return NUM_BLOCKS - usedBlockCount;
    }

    // returns the number of used blocks
    public int getUsedBlockCount() {
        return usedBlockCount;
    }

    // returns the total number of blocks
    public int getTotalBlockCount() {
        return NUM_BLOCKS;
    }

    // returns the size of each block in bytes
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    // returns the total storage size in bytes
    public int getTotalSize() {
        return TOTAL_SIZE;
    }

    // returns the percentage of storage used
    public double getUsagePercentage() {
        return (usedBlockCount * 100.0) / NUM_BLOCKS;
    }

    // utility methods --------------------------------------

    // validates if a block ID is within valid range
    private boolean isValidBlockId(int blockId) {
        return blockId >= 0 && blockId < NUM_BLOCKS;
    }

    // clears all data in a specific block (sets to zero)
    private void clearBlock(int blockId) {
        if (!isValidBlockId(blockId)) {
            return;
        }

        int startPosition = blockId * BLOCK_SIZE;
        Arrays.fill(disk, startPosition, startPosition + BLOCK_SIZE, (byte) 0);
    }

    // formats the entire storage (clears all data and marks all blocks as free)
    public void format() {
        Arrays.fill(disk, (byte) 0);
        Arrays.fill(freeBlocks, true);
        usedBlockCount = 0;
    }

    // returns detailed storage information as string
    public String getStorageInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Storage.Storage Information ===\n");
        sb.append("Block Size: ").append(BLOCK_SIZE).append(" bytes\n");
        sb.append("Total Blocks: ").append(NUM_BLOCKS).append("\n");
        sb.append("Total Size: ").append(TOTAL_SIZE).append(" bytes (")
                .append(TOTAL_SIZE / 1024).append(" KB)\n");
        sb.append("Used Blocks: ").append(usedBlockCount).append("\n");
        sb.append("Free Blocks: ").append(getFreeBlockCount()).append("\n");
        sb.append("Usage: ").append(String.format("%.2f", getUsagePercentage())).append("%\n");
        return sb.toString();
    }

    // short summary string for this storage
    @Override
    public String toString() {
        return String.format("Storage.Storage[blocks=%d, blockSize=%d, used=%d, free=%d]",
                NUM_BLOCKS, BLOCK_SIZE, usedBlockCount, getFreeBlockCount());
    }
}
