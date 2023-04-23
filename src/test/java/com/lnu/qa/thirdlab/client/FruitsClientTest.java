package com.lnu.qa.thirdlab.client;

import com.google.common.collect.Sets;
import com.lnu.qa.thirdlab.generated.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class FruitsClientTest extends AbstractTestNGSpringContextTests {

    private FruitsPort fruitsService;

    private static String getUUID() {
        return UUID.randomUUID().toString();
    }

    @BeforeClass
    public void setUp() {
        log.info("Current thread: {}", Thread.currentThread().getName());
        FruitsPortService fruitsPortService = new FruitsPortService();
        fruitsService = fruitsPortService.getFruitsPortSoap11();
    }

    @AfterMethod
    public void tearDown() {
        fruitsService.removeAllFruits(new RemoveAllFruitsRequest());
    }

    @Test
    public void shouldReturnEmptyListWhenNoFruitsAvailable() {
        Assert.assertEquals(getFruits().size(), 0);
    }

    private List<Fruit> getFruits() {
        return fruitsService.getFruits(new GetFruitsRequest()).getFruits();
    }

    @DataProvider(name = "fruits-data-provider")
    public Object[][] dpMethod() {
        return new Object[][]{
                {getUUID()},
                {getUUID()},
                {getUUID()},
                {getUUID()},
        };
    }

    @Test(dataProvider = "fruits-data-provider")
    public void shouldSaveFruit(String fruitName) {
        //Given
        CreateFruitRequest createFruitRequest = buildCreateFruitRequest(fruitName);
        //When
        Fruit savedFruit = fruitsService.createFruit(createFruitRequest).getFruit();
        //Then
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(savedFruit.getId().length() > 0);
        softAssert.assertEquals(savedFruit.getName(), fruitName);

        List<Fruit> foundFruitsByName = getFruits().stream().filter(fr -> fr.getName().equals(fruitName)).collect(Collectors.toList());
        softAssert.assertEquals(foundFruitsByName.get(0).getId(), savedFruit.getId());
        softAssert.assertAll();
    }

    @Test
    public void shouldGetFruitById() {
        //Given
        String name = getUUID();
        CreateFruitRequest createFruitRequest = buildCreateFruitRequest(name);
        Fruit savedFruit = fruitsService.createFruit(createFruitRequest).getFruit();
        //When
        GetFruitRequest getFruitRequest = new GetFruitRequest();
        getFruitRequest.setId(savedFruit.getId());
        Fruit fruitById = fruitsService.getFruit(getFruitRequest).getFruit();
        //Then
        Assert.assertEquals(fruitById.getName(), savedFruit.getName());
    }

    @Test
    public void shouldRetrieveAllFruits() {
        //Given
        String name_1 = getUUID();
        String name_2 = getUUID();
        //When
        fruitsService.createFruit(buildCreateFruitRequest(name_1));
        fruitsService.createFruit(buildCreateFruitRequest(name_2));
        //Then
        List<Fruit> allFruits = fruitsService.getFruits(new GetFruitsRequest()).getFruits();
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(allFruits.size(), 2);
        softAssert.assertTrue(allFruits.stream().map(Fruit::getName).collect(Collectors.toSet()).equals(Sets.newHashSet(name_1, name_2)));
        softAssert.assertAll();
    }


    @Test
    public void shouldRemoveFruitById() {
        //Given
        String fruitName = getUUID();
        Fruit savedFruit = fruitsService.createFruit(buildCreateFruitRequest(fruitName)).getFruit();
        //When
        RemoveFruitRequest removeFruitRequest = new RemoveFruitRequest();
        removeFruitRequest.setId(savedFruit.getId());
        Fruit fruitById = fruitsService.removeFruit(removeFruitRequest).getFruit();
        //Then
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(fruitById.getName(), savedFruit.getName());
        Optional<Fruit> foundFruitsByName = fruitsService.getFruits(new GetFruitsRequest()).getFruits().stream().filter(fr -> fr.getName().equals(fruitName)).findFirst();
        softAssert.assertFalse(foundFruitsByName.isPresent());
        softAssert.assertAll();
    }
//
    @Test
    public void shouldThrowExceptionWhenAttemptToSaveFruitWithId() {
        //Given
        Fruit fruit = new Fruit();
        fruit.setName(getUUID());
        fruit.setId("id");
        CreateFruitRequest createFruitRequest = new CreateFruitRequest();
        createFruitRequest.setFruit(fruit);
        //Then
        Assert.expectThrows(RuntimeException.class, () -> fruitsService.createFruit(createFruitRequest));
    }

    @Test
    public void shouldReturnNullIfFruitNotFoundById() {
        //Given
        GetFruitRequest getFruitRequest = new GetFruitRequest();
        getFruitRequest.setId("id");
        //When
        Fruit fruit = fruitsService.getFruit(getFruitRequest).getFruit();
        //Then
        Assert.assertNull(fruit);
    }

    @Test
    public void shouldReturnNullIfFruitNotFoundToRemoveById() {
        //Given
        RemoveFruitRequest removeFruitRequest = new RemoveFruitRequest();
        removeFruitRequest.setId("id");
        //When
        Fruit fruit = fruitsService.removeFruit(removeFruitRequest).getFruit();
        //Then
        Assert.assertNull(fruit);
    }

    private CreateFruitRequest buildCreateFruitRequest(String fruitName) {
        Fruit fruit = new Fruit();
        fruit.setName(fruitName);
        CreateFruitRequest createFruitRequest = new CreateFruitRequest();
        createFruitRequest.setFruit(fruit);
        return createFruitRequest;
    }

}
