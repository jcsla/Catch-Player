SRCPATH=.
prefix=/home/jun-young/workspace/FFmpegLibrary/jni/x264/../ffmpeg-build/armeabi
exec_prefix=${prefix}
bindir=${exec_prefix}/bin
libdir=${exec_prefix}/lib
includedir=${prefix}/include
ARCH=ARM
SYS=LINUX
CC=/home/jun-young/android-ndk-r9//toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc-4.4.3 --sysroot=/home/jun-young/android-ndk-r9//platforms/android-5/arch-arm/
CFLAGS=-Wshadow -O3 -fno-fast-math -marm -march=armv5 -Wall -I. -I$(SRCPATH) -std=gnu99 -fomit-frame-pointer -fno-tree-vectorize
DEPMM=-MM -g0
DEPMT=-MT
LD=/home/jun-young/android-ndk-r9//toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc-4.4.3 --sysroot=/home/jun-young/android-ndk-r9//platforms/android-5/arch-arm/ -o 
LDFLAGS=-Wl,-rpath-link=/home/jun-young/android-ndk-r9//platforms/android-5/arch-arm//usr/lib -L/home/jun-young/android-ndk-r9//platforms/android-5/arch-arm//usr/lib -nostdlib -lc -lm -ldl -llog -lm
LIBX264=libx264.a
AR=/home/jun-young/android-ndk-r9//toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86_64/bin/arm-linux-androideabi-ar rc 
RANLIB=/home/jun-young/android-ndk-r9//toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86_64/bin/arm-linux-androideabi-ranlib
STRIP=/home/jun-young/android-ndk-r9//toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86_64/bin/arm-linux-androideabi-strip
AS=
ASFLAGS= -DHIGH_BIT_DEPTH=0 -DBIT_DEPTH=8
RC=
RCFLAGS=
EXE=
HAVE_GETOPT_LONG=1
DEVNULL=/dev/null
PROF_GEN_CC=-fprofile-generate
PROF_GEN_LD=-fprofile-generate
PROF_USE_CC=-fprofile-use
PROF_USE_LD=-fprofile-use
default: cli
install: install-cli
default: lib-static
install: install-lib-static
LDFLAGSCLI = 
CLI_LIBX264 = $(LIBX264)
