description "account semantics"

scenario "increasing an empty account", {

    given "an empty account",{
        sut = new Account()
    }

    when "1 is added", {
        sut.add(BigDecimal.ONE)
    }

    then "the balance should be 1", {
        sut.getBalance().shouldBe BigDecimal.ONE
    }
}