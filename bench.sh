#!/bin/sh

# SPDX-FileCopyrightText: 2012 Emmanuel Pietriga <emmanuel.pietriga@inria.fr>
#
# SPDX-License-Identifier: BSD-4-Clause

java -Djava.library.path=target/lib -cp target/agile2d-3.0.1.jar agile2d.benchmark.BenchmarkGUI
