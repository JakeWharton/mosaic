#ifndef MOSAIC_UTILS_H
#define MOSAIC_UTILS_H

#define likely(x) __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)

#define UNUSED __attribute__((unused))

#if defined(_WIN32)
#define MOSAIC_EXPORT __declspec(dllexport)
#else
#define MOSAIC_EXPORT
#endif

#endif // MOSAIC_UTILS_H
