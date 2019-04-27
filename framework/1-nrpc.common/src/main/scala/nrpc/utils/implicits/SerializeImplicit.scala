package nrpc.utils.implicits

import java.io.{ByteArrayOutputStream, ObjectOutputStream}


case class SerializeImplicit(target: java.io.Serializable) {
  def toBytes(): Array[Byte] = {
    val bos = new ByteArrayOutputStream();
    try {
      val oos = new ObjectOutputStream(bos);
      oos.writeObject(target);
      oos.flush();
      val bytes = bos.toByteArray();
      oos.close();
      bos.close();
      return bytes
    } catch {
      case e: Throwable => e.printStackTrace(); return null
    }
  }
}
