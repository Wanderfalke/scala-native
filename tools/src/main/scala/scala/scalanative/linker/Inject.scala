package scala.scalanative
package linker

import nir.{Global, Defn}

trait Inject {

  /** A sequence of extra dependencies that should additionally be
   *  loaded by linker.
   */
  def depends: Seq[Global] = Seq()

  /** A sequence of extra definitions that should additionally be
   *  injected into final assembly.
   */
  def injects: Seq[Defn] = Seq()
}
