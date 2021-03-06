/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Runs the integration tests with the details configuration (no field deduplication).
 */
public class MetaverseValidationDefaultIT extends MetaverseValidationIT {

  @BeforeClass
  public static void init() throws Exception {
    MetaverseValidationIT.init();
  }

  @Test
  public void testSelectValuesStep() throws Exception {
    testSelectValuesStep( 16 );
  }

  @Test
  public void testTextFileInputNode() throws Exception {
    testTextFileInputNodeImpl( 0 );
  }
}
