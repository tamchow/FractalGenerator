# FractalGenerator
<html>
A <b>Java</b> fractal generator (currently all input is handled through configuration files),
that implements display of custom functions, with constant declaration processing, in both Mandelbrot,Newton, Nova (both Julia and Mandelbrot  types), Secant and Julia modes.

Newton Mode only supports simple polynomials with complex exponents and coefficients allowed, but the polynomial must be in a special format, which is documented in .idea/description.html or the Wiki Description page.

There are 36 different colouring schemes, 15 have linear interpolation, 17 have Catmull-Rom Spline interpolation and 2 which directly render in grayscale for emphasis on inner regions. The final is distance estimation based grayscale or palette coloring, which is slow and has the same limitations as Newton mode as it uses the same mathematics (symbolic differentiation) backend.

Degrees of functions are auto-calculated for use in calculating the renormalized iteration count. The automatic degree calculation system has less issues with grouped operations, especially if considering division or multiplication in the equation. This calculation can be manually overriden after initializing the fractal generator with `setDegree(double)` of the `ComplexFractalGenerator` object in the `Test` class. This could be made more accesible once a GUI is made. Specifying a degree value in the configuration file will also override the calculation of the degree, which can be a complex value. Indexing of colours can be linear or logarithmic. Colors can be interpolated directly or via their R, G and B components.

The spline-interpolant methods are the standard ones of Triangle Area Inequality, Curvature Average and Stripe Average, as well as Histogram coloring and Color Distance Estimation,Orbit and Line Traps and Gaussian Distance and Epsilon Cross, by default.

The linear interpolant methods have the 3 Newton fractal coloring modes, and the division and multiplication direct and normalized coloring modes, and linear versions of the spline-interpolant methods,excepting distance estimation and traps.

Resolution of the fractal is preferred to be in 2m+1x2n+1 format for proper symmetry.

<p>
    NOTES:
    
    Multithreading -- Multithreading works, but it is extremely resource intensive, requiring about a 20x increase in
    memory requirements for about a 10x speedup during generation (12 threads). Also, there might be OOM errors in very
    intensive cases. I still encourage people to use multithreading, with up to 14 threads (7*2).
              
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
  I do not have much time for putting in documenting comments.
      I'll put them in and update files as and when possible.
</p>

<p>
Running:

Parameters are optional in test builds, but not in the release version. If you have a slow PC and it is defaulted to a FullHD or 4K render,
be ready to wait upto an hour or more for output. I have an Asus K55VM and on this configuration with CMD I get 30 mins.

This runs substantially faster through Idea itself (9.5 mins for me), no idea why. I use IntelliJ Idea 15.

You're welcome to use any part of this in any way you wish, but if you acknowledge me I'll be very grateful.

For further details, refer to the wiki.

To run:

<pre>java -jar FractalGenerator.jar [switches] [path-to-fractal-config-file] [options] [output-directory]</pre>

Or, you can just use the included <pre>test.bat</pre> file which is provided along with the <pre>.jar</pre>
Note that a <pre>-test</pre> or <pre>-t</pre> option without switches or other arguments will run the currently-configured test fractal which will be output as: <pre>D:/Fractal.png</pre>
</p>
</html>
