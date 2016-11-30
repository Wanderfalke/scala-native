package scala.scalanative
package optimizer

import tools.Config
import analysis.ClassHierarchy.Top
import nir.{Global, Defn}

trait PassCompanion extends linker.Inject {

  /** Instantiate the given pass. */
  def apply(config: Config, top: Top): Pass
}
