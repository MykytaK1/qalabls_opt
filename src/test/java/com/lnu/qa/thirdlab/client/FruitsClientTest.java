package com.lnu.qa.thirdlab.client;

import com.lnu.qa.thirdlab.generated.*;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class FruitsClientTest {

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
        log.info("Clean test fruits");
        fruitsService.removeAllFruits(new RemoveAllFruitsRequest());
    }

    @Test
    public void shouldReturnEmptyListWhenNoFruitsAvailable() {
        log.info("Run test: shouldReturnEmptyListWhenNoFruitsAvailable");
        Assert.assertEquals(getFruits().size(), 0);
    }

    private List<Fruit> getFruits() {
        return fruitsService.getFruits(new GetFruitsRequest()).getFruits();
    }

    @DataProvider(name = "fruits-name-provider")
    private Object[][] fruitNameDataProvider() {
        return new Object[][]{
                {getUUID()},
                {getUUID()},
                {getUUID()},
                {getUUID()},
        };
    }

    @DataProvider(name = "fruits-names-provider")
    private Object[][] fruitNamesDataProvider() {
        return new Object[][]{
                {getUUID(), getUUID()},
                {getUUID(), getUUID()},
                {getUUID(), getUUID()},
                {getUUID(), getUUID()},
        };
    }

    @Test(dataProvider = "fruits-name-provider")
    public void shouldSaveFruit(String fruitName) {
        log.info("Run test: shouldSaveFruit, name: {}", fruitName);
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

    @Test(dataProvider = "fruits-names-provider")
    public void shouldUpdateFruit(String[] fruitNames) {
        log.info("Run test: shouldUpdateFruit, names: {}", Arrays.toString(fruitNames));
        //Given
        String name_1 = fruitNames[0];
        String name_2 = fruitNames[1];
        CreateFruitRequest createFruitRequest = buildCreateFruitRequest(name_1);
        //When
        Fruit savedFruit = fruitsService.createFruit(createFruitRequest).getFruit();
        //Then
        SoftAssert softAssert = new SoftAssert();
        GetFruitRequest getFruitRequest = new GetFruitRequest();
        getFruitRequest.setId(savedFruit.getId());
        Fruit fruitById = fruitsService.getFruit(getFruitRequest).getFruit();

        softAssert.assertEquals(fruitById.getName(), name_1);
        fruitById.setName(name_2);

        UpdateFruitRequest updateFruitRequest = new UpdateFruitRequest();
        updateFruitRequest.setFruit(fruitById);
        fruitsService.updateFruit(updateFruitRequest);

        GetFruitRequest getFruitRequest_2 = new GetFruitRequest();
        getFruitRequest_2.setId(savedFruit.getId());
        Fruit fruitById_2 = fruitsService.getFruit(getFruitRequest_2).getFruit();
        softAssert.assertEquals(fruitById_2.getName(), name_2);
        softAssert.assertEquals(fruitsService.getFruits(new GetFruitsRequest()).getFruits().size(), 1);
        softAssert.assertAll();
    }

    @Test(dataProvider = "fruits-name-provider")
    public void shouldGetFruitById(String fruitName) {
        log.info("Run test: shouldGetFruitById, name: {}", fruitName);
        //Given
        CreateFruitRequest createFruitRequest = buildCreateFruitRequest(fruitName);
        Fruit savedFruit = fruitsService.createFruit(createFruitRequest).getFruit();
        //When
        GetFruitRequest getFruitRequest = new GetFruitRequest();
        getFruitRequest.setId(savedFruit.getId());
        Fruit fruitById = fruitsService.getFruit(getFruitRequest).getFruit();
        //Then
        Assert.assertEquals(fruitById.getName(), savedFruit.getName());
    }

    @Test(dataProvider = "fruits-names-provider")
    public void shouldRetrieveAllFruits(String[] fruitNames) {
        log.info("Run test: shouldRetrieveAllFruits, names: {}", Arrays.toString(fruitNames));
        //Given
        String name_1 = fruitNames[0];
        String name_2 = fruitNames[1];
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


    @Test(dataProvider = "fruits-name-provider")
    public void shouldRemoveFruitById(String fruitName) {
        log.info("Run test: shouldRemoveFruitById, name: {}", fruitName);
        //Given
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

    @Test
    public void shouldThrowExceptionWhenAttemptToSaveFruitWithId() {
        log.info("Run test: shouldThrowExceptionWhenAttemptToSaveFruitWithId");
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
        log.info("Run test: shouldReturnNullIfFruitNotFoundById");
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
        log.info("Run test: shouldReturnNullIfFruitNotFoundToRemoveById");
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
