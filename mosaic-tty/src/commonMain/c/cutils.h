#ifndef CUTILS_H
#define CUTILS_H

#define likely(x) __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)

#define UNUSED __attribute__((unused))

#if defined(_WIN32)
#define MOSAIC_EXPORT __declspec(dllexport)
#define MOSAIC_STDCALL __stdcall
#else
#define MOSAIC_EXPORT
#define MOSAIC_STDCALL
#endif

#endif // CUTILS_H
