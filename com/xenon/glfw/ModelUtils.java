package com.xenon.glfw;

/**
 * A few static helper methods for models
 * @author Zenon
 */
public class ModelUtils {

    /**
     * Create a quad indices for opengl to draw triangles. Uses the following pattern :
     * <code>
     *     0, 1, 2,<br>
     *     0, 2, 3, <br><br>
     *     4, 5, 6,<br>
     *     4, 6, 7, ...
     * </code>
     * @param quadCount the number of times the pattern is applied. must be less than 32
     * @return the quads' indices
     */
    public static byte[] genQuadIndicesB(int quadCount){
        assert quadCount < 32 : "Exceeding byte size for indices";

        byte[] bytes = new byte[quadCount * 6];

        for ( int i=0; i < quadCount; i++ ){
            int offset = i * 6;
            int indexOffset = i * 4;

            bytes[offset] = bytes[offset + 3] = (byte) indexOffset;
            bytes[offset + 1] = (byte) (indexOffset + 1);
            bytes[offset + 2] = bytes[offset + 4] = (byte) (indexOffset + 2);
            bytes[offset + 5] = (byte) (indexOffset + 3);
        }

        return bytes;
    }

    /**
     * Same as {@link #genQuadIndicesB(int)} but with integers.
     * @see #genQuadIndicesB(int)
     * @param quadCount the number of times the pattern is applied
     * @return the quads' indices
     */
    public static int[] genQuadIndicesI(int quadCount){
        int[] indices = new int[quadCount * 6];

        for ( int i=0; i < quadCount; i++ ){
            int offset = i * 6;
            int indexOffset = i * 4;

            indices[offset] = indices[offset + 3] = indexOffset;
            indices[offset + 1] = indexOffset + 1;
            indices[offset + 2] = indices[offset + 4] = indexOffset + 2;
            indices[offset + 5] = indexOffset + 3;
        }

        return indices;
    }
    /**
     * Same as {@link #genQuadIndicesB(int)} but with shorts.
     * @see #genQuadIndicesB(int)
     * @param quadCount the number of times the pattern is applied
     * @return the quads' indices
     */
    public static short[] genQuadIndicesS(int quadCount){
        short[] indices = new short[quadCount * 6];

        for ( int i=0; i < quadCount; i++ ){
            int offset = i * 6;
            int indexOffset = i * 4;

            indices[offset] = indices[offset + 3] = (short) indexOffset;
            indices[offset + 1] = (short) (indexOffset + 1);
            indices[offset + 2] = indices[offset + 4] = (short) (indexOffset + 2);
            indices[offset + 5] = (short) (indexOffset + 3);
        }

        return indices;
    }
}
