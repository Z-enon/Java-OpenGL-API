
layout (location=0) in vec3 pos;
layout (location=1) in vec4 color;
layout (location=2) in vec3 offset;

out vec4 vcolor;

layout (std140, binding=0) uniform Display
{
    mat4 projMat;   // 64 bytes 0 offset    --> mat4 must be aligned on a 16-byte multiple
    int width;  // 4 bytes  64 offset   --> integer must be aligned on a 4-bytes multiple
    int height; // 4 bytes 68 offset    --> buffer total size 72 bytes
};

void main()
{
    vcolor = color;
    vec3 realPos = pos + offset;
    gl_Position = projMat * vec4(realPos, 1.0);
}