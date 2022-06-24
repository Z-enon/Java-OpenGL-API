#vertex
#inputs <0: vec2 / pos; 1: vec4 / inCol; 2: float / zlevel>
#outputs <0: vec4 / outCol>

#include <XENON_UI_ESSENTIALS>

void main()
{
    outCol = inCol;
    gl_Position = vec4(correct2D( pos ), zlevel, 1.0);
}

#fragment
#inputs <0: vec4 / inCol>
#outputs <0: vec4 / outCol>

void main()
{
    outCol = inCol;
}