import org.w3c.dom.Node
import org.w3c.dom.NodeList

operator fun NodeList.iterator(): Iterable<Node> {
    return Iterable {
        object : MutableIterator<Node> {
            private var index = 0
            override fun hasNext(): Boolean {
                return index < length
            }

            override fun next(): Node {
                if (!hasNext()) throw NoSuchElementException()
                return item(index++)
            }

            override fun remove() {
                throw UnsupportedOperationException()
            }
        }
    }
}