package nrpc.utils.implicits

import java.io.{ByteArrayInputStream, ObjectInputStream}

import nrpc.utils.pattern.PromiseProcessPattern

case class BytesSerializeImplicit(bytes: Array[Byte]) extends PromiseProcessPattern{
  def bytesTo[T](): Option[T] = {
    using(new ByteArrayInputStream(bytes)){ bis =>
      using(new ObjectInputStream(bis)) { ois =>
        val obj = ois.readObject();
        return Some(obj.asInstanceOf[T])
      }
    }
    return None
  }
}
