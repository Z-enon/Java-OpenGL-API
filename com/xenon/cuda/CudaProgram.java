package com.xenon.cuda;

import com.xenon.glfw.abstraction.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.cuda.CUDA;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.cuda.CU.*;
import static org.lwjgl.cuda.NVRTC.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * @author Zenon
 */
public class CudaProgram implements Disposable {

    protected static long context;

    public static CudaProgram launch(String name, String code) {
        start();
        return new CudaProgram(name, code);
    }

    protected static void print(Object... os) {
        for (var o : os)
            System.out.println(o);
    }


    protected static void start() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ib1 = stack.mallocInt(1);
            IntBuffer ib2 = stack.mallocInt(1);
            PointerBuffer pb = stack.mallocPointer(1);

            checkNVRTC(nvrtcVersion(ib1, ib2));
            print("NVRTC version "+ib1.get(0) + "." +ib2.get(0));
            if (CUDA.isPerThreadDefaultStreamSupported())
                Configuration.CUDA_API_PER_THREAD_DEFAULT_STREAM.set(true);
            check(cuInit(0));
            check(cuDeviceGetCount(ib1));
            if (ib1.get(0) == 0)
                throw new IllegalStateException("Error: no devices supporting CUDA");
            check(cuDeviceGet(ib1, 0));
            int device = ib1.get(0);
            ByteBuffer bb = stack.malloc(100);
            check(cuDeviceGetName(bb, device));
            // bb was malloc so there will be a lot of garbage in the buffer.
            // Thus, we need memASCII to loop until it reached a string terminator, and not print the whole buffer,
            // hence memAddress
            print("Using device "+memASCII(memAddress(bb)));

            check(cuDeviceTotalMem(pb, device));
            print("Available memory "+pb.get(0)+" bytes");
            check(cuCtxCreate(pb, 0, device));
            context = pb.get(0);
        }
    }

    protected static void checkNVRTC(int err) {
        if (err != NVRTC_SUCCESS) {
            throw new IllegalStateException(nvrtcGetErrorString(err));
        }
    }
    public static void check(int err) {
        if (err != CUDA_SUCCESS) {
            if (context != NULL) {
                cuCtxDetach(context);
                context = NULL;
            }
            throw new IllegalStateException(Integer.toString(err));
        }
    }

    protected final long func;
    protected final List<Long> devices = new ArrayList<>();


    protected CudaProgram(String name, String code) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pb = stack.mallocPointer(1);
            checkNVRTC(nvrtcCreateProgram(pb, code, name + ".cu", null, null));
            long program = pb.get(0);

            int err = nvrtcCompileProgram(program, null);
            checkNVRTC(nvrtcGetProgramLogSize(program, pb));
            long log_size = pb.get(0);
            if (log_size > 1) {
                ByteBuffer log = stack.malloc((int)log_size - 1);
                checkNVRTC(nvrtcGetProgramLog(program, log));
                System.err.println("Compilation log:");
                System.err.println(memASCII(log));
            }
            checkNVRTC(err);

            checkNVRTC(nvrtcGetPTXSize(program, pb));
            ByteBuffer ptx = memAlloc((int)pb.get(0));
            checkNVRTC(nvrtcGetPTX(program, ptx));
            print("Compilation result: "+memASCII(ptx));
            check(cuModuleLoadData(pb, ptx));
            long module = pb.get(0);
            check(cuModuleGetFunction(pb, module, name));
            func = pb.get(0);
        }
    }


    public long malloc(long size) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pb = stack.mallocPointer(1);
            check(cuMemAlloc(pb, size));
            long pointer = pb.get(0);
            devices.add(pointer);
            return pointer;
        }
    }

    public static void memcpy(long device, ByteBuffer client) {
        check(cuMemcpyHtoD(device, client));
    }
    public static void memcpy(long device, IntBuffer client) {
        check(cuMemcpyHtoD(device, client));
    }
    public static void memcpy(long device, FloatBuffer client) {
        check(cuMemcpyHtoD(device, client));
    }
    public static void memcpy(long device, LongBuffer client) {
        check(cuMemcpyHtoD(device, client));
    }
    public static void memcpy(long device, DoubleBuffer client) {
        check(cuMemcpyHtoD(device, client));
    }

    public static void memcpy(ByteBuffer client, long device) {
        check(cuMemcpyDtoH(client, device));
    }
    public static void memcpy(IntBuffer client, long device) {
        check(cuMemcpyDtoH(client, device));
    }
    public static void memcpy(FloatBuffer client, long device) {
        check(cuMemcpyDtoH(client, device));
    }
    public static void memcpy(LongBuffer client, long device) {
        check(cuMemcpyDtoH(client, device));
    }
    public static void memcpy(DoubleBuffer client, long device) {
        check(cuMemcpyDtoH(client, device));
    }

    public void launchKernel(int gridX, int gridY, int gridZ, int blockX, int blockY, int blockZ) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long[] longs = new long[devices.size()];
            for (int i = 0; i < longs.length; i++)
                longs[i] = memAddress(stack.longs(devices.get(i)));
            PointerBuffer pointers = stack.pointers(longs);
            check(cuLaunchKernel(func, gridX, gridY, gridZ, blockX, blockY, blockZ,
                    0, 0, pointers, null));
        }
    }

    @Override
    public void dispose() {
        for (long device : devices)
            check(cuMemFree(device));

        check(cuCtxDetach(context));
    }
}
