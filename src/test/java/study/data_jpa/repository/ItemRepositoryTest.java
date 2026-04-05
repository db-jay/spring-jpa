package study.data_jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.data_jpa.entity.Item;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemRepositoryTest {

    @Autowired ItemRepository itemRepository;

    @Test
    public void save() {
        // id를 직접 넣었어도 createdAt이 아직 null 이라 isNew() == true 로 판단되는 예제다.
        Item item = new Item("itemA");
        Item savedItem = itemRepository.save(item);

        assertEquals("itemA", savedItem.getId());
        assertTrue(itemRepository.findById("itemA").isPresent());
    }
}
