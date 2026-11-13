/* Copyright (c) 1997-1999 Miller Puckette.
* For information on usage and redistribution, and for a DISCLAIMER OF ALL
* WARRANTIES, see the file, "LICENSE.txt," in this distribution.  */

/* 2026 Antoine Rousseau: adapt [cputime] to measure precisely the CPU usage of the current thread. */
/* renamed [threadtime] */

#include "m_pd.h"
#include <time.h>
#include <sys/times.h>

static t_class *threadtime_class;

typedef struct _threadtime
{
    t_object x_obj;
    long x_setthreadtime;
} t_threadtime;

#define ts_to_ms(ts) (ts.tv_nsec / 1e6 + ts.tv_sec * 1000)

static void threadtime_bang(t_threadtime *x)
{
    struct timespec ts;
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &ts);
    x->x_setthreadtime = ts_to_ms(ts); // in millis
}

static void threadtime_bang2(t_threadtime *x)
{
    t_float elapsedcpu;
    struct tms newthreadtime;
    struct timespec ts;
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &ts);
    elapsedcpu = ts_to_ms(ts) - x->x_setthreadtime;
    outlet_float(x->x_obj.ob_outlet, elapsedcpu);
}

static void *threadtime_new(void)
{
    t_threadtime *x = (t_threadtime *)pd_new(threadtime_class);
    outlet_new(&x->x_obj, gensym("float"));

    inlet_new(&x->x_obj, &x->x_obj.ob_pd, gensym("bang"), gensym("bang2"));
    threadtime_bang(x);
    return (x);
}

void threadtime_setup(void)
{
    threadtime_class = class_new(gensym("threadtime"), (t_newmethod)threadtime_new, 0,
        sizeof(t_threadtime), 0, 0);
    class_addbang(threadtime_class, threadtime_bang);
    class_addmethod(threadtime_class, (t_method)threadtime_bang2, gensym("bang2"), 0);
}

