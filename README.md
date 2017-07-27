# FractalGenerator
For proper project documentation, refer [here](http://tamchow.github.io/FractalGenerator/index.html).

A __Java__ fractal generator (currently all input is handled through configuration files),
that implements display of custom functions, with constant declaration processing, in both Mandelbrot,Newton, Nova (both Julia and Mandelbrot  types), Secant and Julia modes.

Newton Mode only supports simple polynomials with complex exponents and coefficients allowed, but the polynomial must be in a special format, which is documented in .idea/description.html or the Wiki Description page.

There are large number of coloring algorithms, each with its own parameters,
accompanied by both linear and spline interpolation.

##Important Disclaimer
This is a large, old project with me as the sole developer, 
and has organically accrued quite a large amount of related and unrelated code. 

Be aware that most of the code on this project is totally untested except for logic verification - 
I simply no longer have the time. I can only guarantee that `ComplexFractalGenerator` is reasonably tested,
and that too only for the Mandelbrot, Julia and Newton modes.

Also be aware that accompanying documentation can very well be outdated.

Finally, the code is objectively horrible and I apologize for that.
I've since moved to Scala as my primary language, and am no longer going to maintain this.
If you want to see better-written code, you're welcome to take a look at any of my Scala repos.

__TL;DR - This is an Abandoned Project__

##NOTES:
Multithreading is on by default, and can be disabled by setting both `xThreads` and `yThreads` to 1.
Otherwise, this program can and will run very slow, given its expression interpreter.

There is support for zoom, pan and rotate directly during image generation, but note that these can make execution slow.

There is also an included image viewer, which can also display generated fractals, support for elementary image transitions from top, bottom, left and right.
(see the wiki for more details on the options which enable this behaviour).
 
This has been implemented, and is integrated into the main program, with a fractal configuration file path being the only accepted argument.

The parser for image and Fractal display configuration files is a regular language parser, so it'll balk at any errors (with unpredictable results).
However, inline comments beginning  with a `'#'` are supported, _a la_ Python/INI.

Do not expect documentation or comments - there are few, if any, and I don't have the time to put in more.

##Running:
Parameters are optional in test builds, but not in the release version. 
If you have a slow PC and it is defaulted to a FullHD or 4K render,
be ready to wait up to an hour or more for output.

Note that by default the program runs in multithreaded mode utilizing 4 threads, 
and complex fractals may take up to 4 or 5 minutes to render at 1920x1080 resolution.  

You're welcome to use any part of this in any way you wish, but if you acknowledge me I'll be very grateful.

For further details, refer to the wiki.

To run:

    java -jar FractalGenerator.jar [switches] <path-to-fractal-config-file> [options] [output-directory]

Or, you can just use the included `test.bat` file which is provided along with the `JAR`.
Note that a `-test` or `-t` option without switches or other arguments will run the currently-configured test fractal which will be output as: `./Fractal.png` (i.e., as Fractal.png in the directory from which the JAR was launched).
