#ifndef CUTILS_H
#define CUTILS_H

#define likely(x) __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)

#define UNUSED __attribute__((unused))

#endif // CUTILS_H
