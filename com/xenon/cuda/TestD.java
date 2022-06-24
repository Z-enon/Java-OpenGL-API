package com.xenon.cuda;

import java.nio.IntBuffer;

import static com.xenon.cuda.CudaProgram.memcpy;
import static org.lwjgl.system.MemoryUtil.memAllocInt;

/**
 * @author Zenon
 */
public class TestD {

    private static final int ARRAY_SIZE = 100;

    private static final String KERNEL_CU = """
            #define N 100
            extern "C" __global__ void matSum(int *a, int *b, int *c)
            {
                int tid = blockIdx.x;
                if (tid < N)
                    c[tid] = a[tid] + b[tid];
            }
            """;
    private static final String KERNEL_NAME = "matSum";


    public static void main(String[] args) {
        CudaProgram p = CudaProgram.launch(KERNEL_NAME, KERNEL_CU);

        IntBuffer hostA = memAllocInt(ARRAY_SIZE);
        IntBuffer hostB = memAllocInt(ARRAY_SIZE);
        IntBuffer hostC = memAllocInt(ARRAY_SIZE);

        // initialize host arrays
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            hostA.put(i, ARRAY_SIZE - i);
            hostB.put(i, i * i);
        }

        long
                deviceA,
                deviceB,
                deviceC;

        deviceA = p.malloc(Integer.BYTES * ARRAY_SIZE);
        deviceB = p.malloc(Integer.BYTES * ARRAY_SIZE);
        deviceC = p.malloc(Integer.BYTES * ARRAY_SIZE);

        memcpy(deviceA, hostA);
        memcpy(deviceB, hostB);

        p.launchKernel(
                ARRAY_SIZE, 1, 1, 1, 1, 1
        );

        System.out.format("# Kernel complete.\n");

        memcpy(hostC, deviceC);
        for (int i = 0; i < ARRAY_SIZE; ++i) {
            System.out.println(hostC.get(i));
        }
        System.out.format("*** All checks complete.\n");
    }
}
