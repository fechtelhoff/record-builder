# Record Builder

Simple Annotation based Builder Creation for Records.

Inspired by: https://github.com/javahippie/jukebox (THX Tim!)

## Links
- https://www.baeldung.com/java-default-annotations
- https://www.baeldung.com/java-custom-annotation
- https://www.baeldung.com/java-annotation-processing-builder

## Note
Take care on the compilation order!

The Builder has to be compiled before he can be used in other projects. (Simple stupid statement but it's true.)

Here this is done by putting the Builder and the Part where it will be used, in different Maven submodules.
