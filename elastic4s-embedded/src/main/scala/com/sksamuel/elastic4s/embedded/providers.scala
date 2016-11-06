package com.sksamuel.elastic4s.embedded

import java.nio.file.{Path, Paths}
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

import com.sksamuel.elastic4s.ElasticClient

// LocalNodeProvider provides helper methods to create a local (embedded) node
trait LocalNodeProvider {

  // returns a client connected to a local node.
  def client: ElasticClient = node.elastic4sclient()

  // returns an embedded, started, node
  def node: LocalNode
}

// implementation of LocalNodeProvider that uses a single
// node instance for all classes in the same classloader.
trait ClassloaderLocalNodeProvider extends LocalNodeProvider {
  override val client: ElasticClient = ClassloaderLocalNodeProvider.client
  override val node: LocalNode = ClassloaderLocalNodeProvider.node
}

object ClassloaderLocalNodeProvider {

  private lazy val tempDirectoryPath: Path = Paths get System.getProperty("java.io.tmpdir")
  private lazy val pathHome: Path = tempDirectoryPath resolve UUID.randomUUID().toString

  lazy val node = LocalNode("classloader-node", pathHome.toAbsolutePath.toString)
  lazy val client = node.elastic4sclient()
}

// implementation of LocalNodeProvider that uses a single
// node instance for each class that mixes in this trait.
trait ClassLocalNodeProvider extends LocalNodeProvider {

  private lazy val tempDirectoryPath: Path = Paths get System.getProperty("java.io.tmpdir")
  private lazy val pathHome: Path = tempDirectoryPath resolve UUID.randomUUID().toString

  override lazy val node = LocalNode(
    "node_" + ClassLocalNodeProvider.counter.getAndIncrement(),
    pathHome.toAbsolutePath.toString
  )

  override lazy val client: ElasticClient = node.elastic4sclient()
}

object ClassLocalNodeProvider {
  val counter = new AtomicLong(1)
}
