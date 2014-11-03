Workflow for Java
=========================

[![Build Status](https://travis-ci.org/mediascience/java-workflow.svg)](https://travis-ci.org/mediascience/java-workflow)

Simple and maintainable programs for complex, parallel, 
distributed work.


## Goals

This project is part of an effort to replace Amazon Flow Framework for Java
as an interface to Amazon SWF. It will attempt to address some difficulties
and shortcomings of Flow Framework. 

SWF itself is well-suited to large-scale distributed parallel processing.
In this project, we will attempt to provide both a model for programming
these kinds of systems as well as a secific implementation for SWF. The
goals include:

1. unified programming model applicable to multiple workflow back ends
1. development-oriented back end that runs in a local JVM
1. SWF back end
1. no byte-code enhancement
1. no code generation
1. optional sugar via Java Dynamic Proxy
1. common side effects such as logging, time, and random number generation
1. a means to inject custom local side effects
1. meta-controls such as queue selection and sub-jobs
1. signals support
1. retry support
1. stateless workers
1. worker watchdog
1. worker lifecycle
    * stop
    * drain
    * die

The programming model will not try to
simulate imperative programming. It will embrace continuation-passing
style with java-promise. It will try to detect model violations at
compile-time and fail fast at runtime where that is not possible. It
will be itself testable and promote the development of testable programs
using it.

It will adopt an event-sourcing model to represent job state so that
any available worker can recreate the current state by fetching the
event stream.

The system may prescribe a common data serialization method and common
error classification scheme.

## So far

The code so far implements a toy event-sourced back end that deals with
only the happy path. It shows how progressive application of a growing
event stream permits a promise-based job specification to drive parallel
processing.




