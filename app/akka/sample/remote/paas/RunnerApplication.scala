package sample.remote.paas

import scala.concurrent.duration._
import scala.util.Random
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props

object RunnerApplication {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.head == "Master")
      startRemoteMasterSystem()
    if (args.isEmpty || args.head == "Slave"){
      if (args.length > 1){
        startRemoteSlaveSystem(args(1))
      } else {
        startRemoteSlaveSystem("127.0.0.1")
      }
    }
  }

  def startRemoteMasterSystem(): Unit = {
    val system = ActorSystem("MasterSystem",
      ConfigFactory.load("master"))
    system.actorOf(Props[MasterActor], "master")

    println("Started MasterSystem - waiting for messages")
  }

  def startRemoteSlaveSystem(masterIP: String): Unit = {
    val system =
      ActorSystem("SlaveSystem", ConfigFactory.load("slave"))
    val remoteMasterPath =
      "akka.tcp://MasterSystem@" + masterIP + ":2552/user/master"
    val actor = system.actorOf(Props(classOf[SlaveActor], remoteMasterPath), "slave")

    println("Started SlaveSystem")
    import system.dispatcher
    system.scheduler.schedule(1.second, 5.second) {
      println("###Master, tell me something interesting please!\n")
      actor ! TellMeSomethingMyMaster()
    }
  }
}