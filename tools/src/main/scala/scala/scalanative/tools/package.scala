package scala.scalanative

import java.nio.file.Paths
import scalanative.io.withScratchBuffer
import scalanative.nir.Global

// API use-cases
//
// sbt plugin:
//   1. link & compile code
//
// compiler  test : VirtualDirectory -> World
//   1. virtual file system of some kind
//   2. instantiate scalac+nscplugin
//   3. run it and check invariants
//
// optimizer test : World -> World
//   1. define what passes we want
//   2. run them and check invariants
//
// code gen test : World -> Seq[String]
//   1. run code gen and check the string

package object tools {
  type LinkerPath = linker.Path
  val LinkerPath = linker.Path

  type LinkerReporter = linker.Reporter
  val LinkerReporter = linker.Reporter

  type LinkerResult = (Seq[nir.Global], Seq[nir.Attr.Link], Seq[nir.Defn])

  type LinkerInject = linker.Inject

  type OptimizerDriver = optimizer.Driver
  val OptimizerDriver = optimizer.Driver

  type OptimizerReporter = optimizer.Reporter
  val OptimizerReporter = optimizer.Reporter

  /** Given the classpath and entry point, link under closed-world assumption. */
  def link(config: Config,
           driver: OptimizerDriver,
           reporter: LinkerReporter): LinkerResult = {
    val paths   = config.paths
    val injects = driver.passes
    val entries =
      Seq(nir.Global.Member(config.entry, "main_class.ssnr.ObjectArray_unit"))

    link(paths, injects, reporter, entries)
  }

  /** Fully explicit linker invocation. */
  def link(paths: Seq[LinkerPath],
           injects: Seq[LinkerInject],
           reporter: LinkerReporter,
           entries: Seq[Global]): LinkerResult =
    linker.Linker(paths, injects, reporter).link(entries)

  /** Transform high-level closed world to its lower-level counterpart. */
  def optimize(
      config: Config,
      driver: OptimizerDriver,
      assembly: Seq[nir.Defn],
      reporter: OptimizerReporter = OptimizerReporter.empty): Seq[nir.Defn] =
    optimizer.Optimizer(config, driver, assembly, reporter)

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config, assembly: Seq[nir.Defn]): Unit = {
    val gen = scalanative.codegen.CodeGen(assembly)

    withScratchBuffer { buffer =>
      gen.gen(buffer)
      buffer.flip
      config.targetDirectory.write(Paths.get("out.ll"), buffer)
    }
  }
}
