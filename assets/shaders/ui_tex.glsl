#vertex
#inputs <0: vec2 / pos; 1: vec2 / inUVs; 2: uvec2 / inTexID; 3: float / zlevel>
#outputs <0: vec2 / outUVs; 1[flat]: uvec2 / outTexID>

#include <XENON_UI_ESSENTIALS>

void main()
{
    outUVs = inUVs;
    outTexID = inTexID;
    gl_Position = vec4(correct2D( pos ), zlevel, 1.0);
}

#fragment
#extension GL_ARB_bindless_texture : require
#inputs <0: vec2 / inUVs; 1[flat]: uvec2 / inTexID>
#outputs <0: vec4 / outCol>

void main()
{
    sampler2D s = sampler2D(inTexID);
    outCol = texture(s, inUVs);
    if (outCol.a < 0.1)
        discard;
}