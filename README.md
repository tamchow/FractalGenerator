# FractalGenerator
<html>
A <b>Java</b> fractal generator (currently without an input/configuration UI, soon through a configuration file),
that implements display of custom functions, with constant declaration processing, in both Mandelbrot and Julia modes.

Newton Mode only supports simple polynomials with complex exponents and coefficients allowed, but the polynomial must be in a special format, which is documented in .idea/description.html

There are 9 different colouring schemes, 4 have linear interpolation, 3 have Catmull-Rom Spline interpolation and 2 which directly render in grayscale for emphasis on inner regions.

The spline-interpolant methods are the standard ones of Triangle Area Inequality, Curvature Average and Stripe Average.

The linear interpolant methods have the 2 Newton fractal special modes, and the division and multiplication direct generation modes.

Resolution of the fractal is preferred to be in 2m+1x2n+1 format for proper symmetry.

<p>
    NOTES:
    The fractal generator has the basic framework for being extensible and being multithreaded, but it is currently single-threaded since I'm not really sure about how well or correctly I can handle multithreading.
    
    Also,there is full support for zoom, but renders after zoom are slow for higher base precision values. As a thumb rule, for quadrupling total resolution, double base precision for best results.
</p>

<p>
   There is also an included image viewer, which can also display fractals.
    There is support for elementary image transitions from top,bottom,left and right.
    NOTE:
    Both are supported now in code but are untested.
    
    This has been implemented, and is integrated into the main program, with a fractal configuration file path being the only accepted argument.
    
    Image and Fractal display configuration files are a stickler for format, but do support inline comments with "#", as in .ini files.
</p>

<p>
  I did not have time to put in documenting comments. I'll start putting them in and updating files as they get ready.
</p>
</html>
