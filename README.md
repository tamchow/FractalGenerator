# FractalGenerator
<html>
A <b>Java</b> fractal generator (currently without an input/configuration UI, soon through a configuration file),
that implements display of custom functions, with constant declaration processing, in both Mandelbrot,Newton Julia modes.

Newton Mode only supports simple polynomials with complex exponents and coefficients allowed, but the polynomial must be in a special format, which is documented in .idea/description.html

There are 10 different colouring schemes, 4 have linear interpolation, 3 have Catmull-Rom Spline interpolation and 2 which directly render in grayscale for emphasis on inner regions. The final is distance estimation based black-and-white coloring, which is slow and has the same limitations as Newton mode as it uses the same mathematics (symbolic differentiation) backend.

Degrees of functions are auto-calculated for use in calculating the renormalized iteration count. The automatic degree calculation system has some issues with nested powers, especially if considering division or multiplication in-system. This calculation can be manually overriden after initializing the fractal generator with `setDegree(double)` of the `FractalGenerator` object in the `Main` class. This could be made more accesible once a GUI is made.

The spline-interpolant methods are the standard ones of Triangle Area Inequality, Curvature Average and Stripe Average.

The linear interpolant methods have the 2 Newton fractal standard coloring modes, and the division and multiplication direct coloring modes.

Resolution of the fractal is preferred to be in 2m+1x2n+1 format for proper symmetry.

<p>
    NOTES:
    
    Multithreading -- A class enabling multithreaded fractal generation has been included, but there is no output.
    Will have to be debugged.
              
    Also,there is full support for zoom, but renders after zoom are slow for higher base precision values. As a thumb rule, for quadrupling total resolution, double base precision for best results. Also, total scaling beyond 10<sup>4</sup> might not produce very detailed output, but then I haven't done very extensive tests.
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

<p>
To run:

`java -jar FractalGenerator.jar [path-to-fractal-config-file]`

Parameter is optional, but if you have a slow PC and it is defaulted to a FullHD render,
be ready to wait upto an hour for output. I have an Asus K55VM and on this configuration with CMD I get 30 mins.

This runs substantially faster through Idea itself (9.5 mins for me), no idea why. I use IntelliJ Idea 15.

You're welcome to use any part of this in any way you wish, but if you acknowledge me I'll be very grateful.

For further details, refer to `/.idea/description.html`.
</p>
</html>
