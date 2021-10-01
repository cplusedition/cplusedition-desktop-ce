dnl Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT(unixsocket-cc, 1.0)
AC_PROG_CC
AC_PROG_CXX
AC_CONFIG_HEADER([src/config.h])
AC_CONFIG_MACRO_DIR([m4])

AC_CANONICAL_SYSTEM
dnl Initialize automake
AM_INIT_AUTOMAKE

dnl Initialize Libtool
LT_INIT

AC_CHECK_HEADERS([arpa/inet.h fcntl.h inttypes.h netinet/in.h stdint.h stdlib.h string.h sys/socket.h syslog.h unistd.h])

dnl Check if Libtool is present
dnl Libtool is used for building share libraries 
AC_PROG_LIBTOOL

AC_CHECK_LIB(pthread, pthread_mutex_lock)

AC_CONFIG_FILES(Makefile src/Makefile)
AC_OUTPUT
