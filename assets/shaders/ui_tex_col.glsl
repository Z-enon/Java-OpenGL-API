#vertex
#inputs <0: vec2 / pos; 1: vec2 / inUVs; 2: vec4 / inCol; 3: uvec2 / inTexID; 4: float / zlevel>
#outputs <0: vec4 / outCol; 1: vec2 / outUVs; 2[flat]: uvec2 / outTexID>

#include <XENON_UI_ESSENTIALS>

void main()
{
    outCol = inCol;
    outUVs = inUVs;
    outTexID = inTexID;
    gl_Position = vec4(correct2D( pos ), zlevel, 1.0);
}

#fragment
#extension GL_ARB_bindless_texture : require
#inputs <0: vec4 / inCol; 1: vec2 / inUVs; 2[flat]: uvec2 / inTexID>
#outputs <0: vec4 / outCol>

void main()
{
    sampler2D s = sampler2D(inTexID);
    outCol = inCol * texture(s, inUVs);
    if (outCol.a < 0.1)
        discard;
}