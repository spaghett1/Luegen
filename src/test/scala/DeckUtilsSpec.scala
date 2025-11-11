import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DeckUtilsSpec extends AnyWordSpec with Matchers{
  import DeckUtils._
  
  
  "DeckUtils" should {
    "create a full deck of cards" in {
      val received = createDeck()
      received.size shouldBe 52
      received.distinct.size shouldBe 52
    }
    
    "shuffle a created deck without losing or adding cards" in {
      val received = createDeck()
      val shuffled = shuffle(received)
      
      received.size shouldBe shuffled.size
      received.toSet shouldBe shuffled.toSet
    }
    
    
  }

}
