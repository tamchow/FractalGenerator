# FractalGenerator
<html>
A <b>Java</b> fractal generator (currently without an input/configuration ui, soon through a .ini file),
that implements display of custom functions,
with constant declaration processing, in both Mandelbrot and Julia modes.
There are 6 different colouring schemes, 5 of which use iteration renormalization with linear interpolation for emphasis on outer regions,
and 1 which renders in grayscale for emphasis on inner regions.
Resolution of the fractal is preferred to be in 2m+1x2n+1 format for proper symmetry.
<p>
    NOTES:
    The fractal generator has the basic framework for being extensible and being multithreaded, but it is currently single-threaded since I'm not
    really sure about how well or correctly I can handle multithreading.
    Also,there is full support for zoom, but renders after zoom are slow if the input resolution is higher than 401x401 and very slow above 801x801.
    Not really sure about threaded prerenders, will have to ask someone for help with it.
</p>

<p>
   There is also an included image viewer, which can also display fractals.
    There is support for elementary image transitions from top,bottom,left and right.
    However, saving of the fractal image and zooming in the viewer is not currently supported(at the top of my TODO list).
    NOTE:
    Both are supported now in code but are untested.
    I will soon implement configuring fractal display through a configuration file.
    NOTE:
    This has been implemented, but is not integrated anywhere in the main program.
    Image display configuration is a stickler for format, but is pretty much ready.
    Same for the fractal configuration.
</p>

<p>
  I did not have time to put in documenting comments. I'll start putting them in and updating files as they get ready.
</p>
</html>
