public final class GameEconomyServiceTest {
    public static void main(String[] args) {
        GameEconomyService service = new GameEconomyService();
        GameEconomyService.Asset pending = service.submitAsset("student-player", "asset-1", "Lesson Arena");
        if (service.canEnterLiveEvent(pending)) throw new AssertionError("pending asset entered live event");
        GameEconomyService.Asset approved = service.approveNextAsset();
        if (!service.canEnterLiveEvent(approved)) throw new AssertionError("approved asset was rejected");
        System.out.println("GameEconomyServiceTest passed: pending rejected, approved admitted");
    }
}
