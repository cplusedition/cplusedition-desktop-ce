#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>

#include "UnixSocket.h"

#define DEBUG_ 0
#define ERROR_(cond, msg) \
	do { \
	    if (cond) { if (DEBUG_) perror(msg); return -1; } \
	} while(0)

/* Map constant in UnixSocket SOCK_DGRAM and SOCK_STREAM to the C constants. */
#define SOCK_TYPE(type) ((type) == 0 ? SOCK_DGRAM : SOCK_STREAM)

JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_listen(JNIEnv * env, jclass jClass, jstring jSockFile, jint jSockType,
	jint jBacklog) {
	int s;
	struct sockaddr_un sa;
	socklen_t salen;
	const char *socketFile = (*env)->GetStringUTFChars(env, jSockFile, NULL);
	ERROR_(strlen(socketFile) >= 104, "Native.open: socket path too long");
	s = socket(PF_UNIX, SOCK_TYPE(jSockType), 0);
	ERROR_(s == -1, "Native.listen: socket");
	bzero(&sa, sizeof(sa));
	sa.sun_family = AF_UNIX;
	strcpy(sa.sun_path, socketFile);
#if !defined(__FreeBSD__)
	salen = strlen(sa.sun_path) + sizeof(sa.sun_family);
#else
	salen = SUN_LEN(&sa);
	sa.sun_len = salen;
#endif
	ERROR_(bind(s, (struct sockaddr *)&sa, salen) == -1, "Native.listen: bind");
	if (SOCK_TYPE(jSockType) == SOCK_STREAM) {
		ERROR_(listen(s, jBacklog) == -1, "Native.listen: listen");
	}
	(*env)->ReleaseStringUTFChars(env, jSockFile, socketFile);
	return s;
}

JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_accept(JNIEnv * env, jclass jClass, jint jSockHandle, jint jSockType) {
	int s = -1;
	ERROR_(jSockHandle == -1, "Native.accept: socket");
	if (SOCK_TYPE(jSockType) == SOCK_STREAM) {
		s = accept(jSockHandle, NULL, 0);
		ERROR_(s == -1, "Native.accept: accept");
	}
	return s;
}

JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_open(JNIEnv * env, jclass jClass, jstring jSockFile, jint jSockType) {
	int s;
	struct sockaddr_un sa;
	socklen_t salen;
	const char *socketFile = (*env)->GetStringUTFChars(env, jSockFile, NULL);
	ERROR_(strlen(socketFile) >= 104, "Native.open: socket path too long");
	s = socket(PF_UNIX, SOCK_TYPE(jSockType), 0);
	ERROR_(s == -1, "Native.open: socket");
	bzero(&sa, sizeof(sa));
	sa.sun_family = AF_UNIX;
	strcpy(sa.sun_path, socketFile);
#if !defined(__FreeBSD__)
	salen = strlen(sa.sun_path) + sizeof(sa.sun_family);
#else
	salen = SUN_LEN(&sa);
	sa.sun_len = salen;
#endif
	ERROR_(connect(s, (struct sockaddr *)&sa, salen) == -1, "Native.open: connect");
	(*env)->ReleaseStringUTFChars(env, jSockFile, socketFile);
	return s;
}

/** @return 0 if EOF, -1 if error, otherwise number of bytes read. */
JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_read(JNIEnv * env, jclass jClass, jint jSockHandle, jbyteArray jbarr,
	jint off, jint len) {
	ssize_t count;
	jbyte *cbarr = (*env)->GetByteArrayElements(env, jbarr, NULL);
	ERROR_(cbarr == NULL, "Native.read: GetByteArrayElements");
	count = read(jSockHandle, &cbarr[off], len);
	ERROR_(count == -1, "Native.read: read");
	(*env)->ReleaseByteArrayElements(env, jbarr, cbarr, 0);
	return count;
}

JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_write(JNIEnv * env, jclass jClass, jint jSockHandle, jbyteArray jbarr,
	jint off, jint len) {
	ssize_t count;
	jbyte *cbarr = (*env)->GetByteArrayElements(env, jbarr, NULL);
	ERROR_(cbarr == NULL, "Native.write: GetByteArrayElements");
	count = write(jSockHandle, &cbarr[off], len);
	ERROR_(count == -1, "Native.write: write");
	(*env)->ReleaseByteArrayElements(env, jbarr, cbarr, JNI_ABORT);
	return count;
}

JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_close(JNIEnv * env, jclass jClass, jint jSockHandle) {
	shutdown(jSockHandle, SHUT_RDWR);
	return close(jSockHandle);
}

JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_closeInput(JNIEnv * env, jclass jClass, jint jSockHandle) {
	return shutdown(jSockHandle, SHUT_RD);
}

JNIEXPORT jint JNICALL
Java_sf_unixsocket_UnixSocket_00024Native_closeOutput(JNIEnv * env, jclass jClass, jint jSockHandle) {
	return shutdown(jSockHandle, SHUT_WR);
}

//JNIEXPORT jint JNICALL
//Java_sf_unixsocket_UnixSocket_00024Native_unlink(JNIEnv * env, jclass jClass, jstring jSockFile) {
//	int ret;
//	const char *socketFile = (*env)->GetStringUTFChars(env, jSockFile, NULL);
//	ret = unlink(socketFile);
//	(*env)->ReleaseStringUTFChars(env, jSockFile, socketFile);
//	return ret;
//}
