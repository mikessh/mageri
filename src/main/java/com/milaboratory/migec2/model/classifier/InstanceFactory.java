/*
 * Copyright 2013-2014 Mikhail Shugay (mikhail.shugay@gmail.com)
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
 *
 * Last modified on 23.11.2014 by mikesh
 */

package com.milaboratory.migec2.model.classifier;

import com.milaboratory.migec2.model.variant.Variant;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface InstanceFactory {
    public void save(File outputFile) throws IOException;

    public boolean hasInstances();

    public Instances getDataset();

    public List<Variant> getVariants();

    public Instance convert(Variant variant);

    public Instance convertAndStore(Variant variant, boolean real);
}
