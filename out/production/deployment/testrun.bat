@echo off
java -server -XX:FreqInlineSize=1000 -XX:CompileThreshold=1500 -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:+UseStringCache -XX:+UseParallelOldGC -XX:AllocatePrefetchStyle=2 -XX:+OptimizeStringConcat -Xms1536m -Xmx2048m -XX:PermSize=128m -XX:MaxPermSize=1024m -jar FractalGenerator.jar -t 2 4
pause