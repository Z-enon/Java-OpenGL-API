# Java-OpenGL-API
A few abstraction layers for LWJGL OpenGL. From shader light pre-processor to UI handling.

WARNING: If you are too lazy to read everything, I must nevertheless warn you that for some reasons blending still isn't working at all, despite testing several strategies. The test can be found in ```com.xenon.test.Test.java```.

## Rudimentary layer
Basic layer all LWJGL developer needs. Is located in ```com.xenon.glfw```.
Provides:
- an abstraction for GLFW context, for automatic shader feeding (no need to manually write "#version 450 core")
- simple abstraction for GLFW windows and shader programs
- a sample for beginner OpenGL on Java: ```Sample.java```. Note that for this sample, ```Mesh.java``` no longer exists, because it was just a ByteBuffer wrapper doing nothing much.
- a few miscellaneous static methods located in ```GLTools.java``` to bind objects only when necessary, and also a few buffer wrapping methods.

This layer does help a bit with initialization, but vertex data feeding and drawing is still vanilla-like.

## Extensive layer
The second layer is more about abstracting the whole drawing and mesh thing. It takes the form of batches with different VertexFormats. Is located in ```com.xenon.opengl```.
Provides:
- an abstraction for drawing stuff, located in ```com.xenon.opengl.abstraction```. ```WorldRenderer.java``` is the base interface, ```AbstractQuadRenderer.java``` provides actual buffers and MDI drawing strategy and ```Renderers.java``` contains all the default 2D renderers, respectively for colored quads, textured quads and colored textured quads.
- a few static methods for drawing rectangles less verbosely than manual WorldRenderer thing, provided by ```RenderUtils.java```.
- an abstraction for the VertexFormat client-side (not GPU shader-side). ```VertexFormat.java``` effectively describes how vertex data will be passed to the vertex shader, therefore it allows automatic attrib pointer using DSA (Direct State Access) via its ```attribSetup(int vao)``` method.
- a shader pre-processing feature, located in ```com.xenon.opengl.debug```. ```Circe.java``` parses special headers in the shaders to ensure matching inputs/outputs throughout shader stages and even to ensure that client-side outputs, described by ```DataFormatElement.java``` (which ```VertexFormat.java``` is composed of) matches vertex shader inputs. ```Polypheme.java``` provides a library feature to prevent methods copy-paste, thus improving maintainability. Correct syntax for "my" shaders is specified in those two classes in the comments; Especially, in ```Circe.java```, in ```handleOutputHeader``` and ```handleIntputHeader``` methods.
- aside from those shader pre-processing utils ```Polypheme.java``` and ```Circe.java```, ```com.xenon.opengl.debug``` contains a few classes that help debugging such as ```DebugContext.java``` that is hiding KHR_debug introduced in GL43, and ````TestUnit.java``` which is... a test unit.

## Final UI layer
The final layer provides basic UI abstraction. Is located in ```com.xenon.ui```. Provides:
- a few abstractions in ```com.xenon.ui.abstraction``` that I will let you discover by yourself :).
- a kind of canvas/viewport feature (or iframe in HTML) provided by ```UIContexts.java```. An example of its use can be found in ```com.xenon.test.Test.java```.

This layer isn't finished at all, and I don't know when it'll be since I plan to switch to Vulkan soon. Anyway, the major interest of this layer is ```UIContexts.java```.

## Final words
This is no graphic engine! The solutions I propose work fairly well (z-testing, ARB bindless on my computer) expect for blending (very sad indeed). Cutout objects can easily be taken care of using manual alpha testing in the fragment shader, but I must have forgotten something with blending. See ya with Vulkan next time. 
