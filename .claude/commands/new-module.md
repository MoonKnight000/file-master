# /new-module — Yangi modul scaffold qilish

`$ARGUMENTS` — modul nomi (masalan: `payment`, `notification`, `report`)

Berilgan nom asosida `uz.murodjon.filemaster.<nom>/` papkasida to'liq modul yaratt.
Quyidagi BARCHA fayllarni yaratish MAJBURIY — birortasini qoldirma:

## Yaratilishi kerak bo'lgan fayllar

```
<nom>/
  controller/
    <Name>Controller.kt          ← interface, mapping annotatsiyalar shu yerda
    impl/
      <Name>ControllerImpl.kt   ← @RestController, annotatsiyasiz override-lar
  service/
    <Name>Service.kt             ← interface
    <Name>ServiceImpl.kt        ← @Service, @Transactional, logika shu yerda
  model/
    <Name>.kt                    ← @Entity, Long id IDENTITY, active Boolean, createdTimestamp/updatedTimestamp Long
  repository/
    <Name>Repository.kt         ← interface : JpaRepository, findAllByActiveTrue()
  dto/
    <Name>Request.kt
    <Name>Response.kt
```

## Qat'iy qoidalar

1. **Har bir top-level class — alohida fayl.** Hech qachon bir faylga 2 ta class yozma.
2. **Controller interface** — `@RequestMapping("/v1/<nom>s")`, `@GetMapping`, `@PostMapping` va boshqa annotatsiyalar faqat interfaceda. Impl-da `@RestController` dan boshqa mapping annotatsiya YO'Q.
3. **Har bir endpoint** `ResponseEntity<ResponseData<T>>` qaytaradi. `ResponseData.success(data)` ishlatiladi.
4. **ServiceImpl** — `@Service @Transactional` + interfacedan implement qiladi. Hech qanday logika controllerda bo'lmaydi.
5. **Entity** — `@Entity @Table(name="<nom>s")`, `@Id @GeneratedValue(strategy=IDENTITY) val id: Long = 0`, `var active: Boolean = true`, `var createdTimestamp: Long = 0`, `var updatedTimestamp: Long = 0`.
6. **Repository** — `findAllByActiveTrueOrderByCreatedTimestampDesc()` ni qo'sh.
7. **Exceptions** — `ExcCode` ga yangi entry qo'sh (masalan `<NAME>_NOT_FOUND`), `class <Name>NotFoundException` yaratt.
8. **Scaffold tugagach** — `./gradlew compileKotlin` ishlatib compile xatolarini tekshir va to'g'irla.

## Misol chaqiruv

```
/new-module payment
```

Bu `payment/` papkasida `PaymentController`, `PaymentService`, `Payment` entity va boshqa barcha fayllarni yaratadi.
