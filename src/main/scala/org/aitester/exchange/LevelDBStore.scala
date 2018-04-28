package org.aitester.exchange

import org.iq80.leveldb._
import org.iq80.leveldb.impl._
import java.io._

class LevelDBStore(filename: String) {
  import org.iq80.leveldb.impl.Iq80DBFactory.factory
  import org.iq80.leveldb.impl.Iq80DBFactory.bytes

  val options = new Options()
  options.createIfMissing(true)
  val db = factory.open(new File(filename), options)

  def close = db.close()

  def putInDB(key: String, value: String) = db.put(bytes(key), bytes(value))

  def getFromDB(key: String) = {
    val value = db.get(bytes(key))
    if(value != null)
      Some(new String(value))
    else
      None
  }

  def putInDBBatch(putBatch : Seq[(String, String)]) = {
    val batch = db.createWriteBatch()
    putBatch.foreach(_ match {
      case (key, value) => batch.put(bytes(key), bytes(value))
    })
    db.write(batch)
  }

  def deleteFromDB(key:String) = db.delete(bytes(key))

  def deleteFromDBBatch(deleteBatch: Seq[String]) = {
    val batch = db.createWriteBatch()
    deleteBatch.foreach(str => batch.delete(bytes(str)))
    db.write(batch)
  }
}
