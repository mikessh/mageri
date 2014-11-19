/**
 * Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.milaboratory.migec2.benchmark;

import com.milaboratory.migec2.core.align.processor.aligners.ExtendedExomeAlignerFactory;
import com.milaboratory.migec2.datasim.MigGenerator;
import com.milaboratory.migec2.datasim.hotspot.HotSpotMigGenerator;

public class BenchmarkMain {
    public static void main(String[] args) throws Exception {
        MigGenerator migGenerator = new HotSpotMigGenerator();

        //BenchmarkRunner benchmarkRunner = new BenchmarkRunner(migGenerator, 1000, new ExtendedExomeAlignerFactory());
        //benchmarkRunner.run();

        System.out.println(migGenerator);
    }
}
