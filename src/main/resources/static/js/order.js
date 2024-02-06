function order(items) {

  function createItem(item = { productId: "", quantity: "" }) {
    return {
      productId: item.productId,
      price: "",
      quantity: item.quantity,
      get amount() {
        const amount = this.price * this.quantity;
        return amount || "";
      }
    };
  }

  return {
    items: items ? items.map(item => createItem(item)) : [createItem()],
    addItem() {
      this.items.push(createItem());
    },
    editItem(index, option) {
      const item = this.items[index];
      item.productId = option.value;
      item.price = option.dataset.price;
      if (item.quantity === "") item.quantity = 1;
    },
    removeItem(index) {
      this.items.length > 1 && this.items.splice(index, 1);
    },
    containsDuplicates() {
      const seen = new Set();
      for (const item of this.items) {
        const propertyValue = item["productId"];
        if (seen.has(propertyValue) && propertyValue !== "") {
          return true;
        }
        seen.add(propertyValue);
      }
      return false;
    },
    get totalPrice() {
      return this.items
        .map(item => item.amount)
        .reduce((prev, next) => prev + next);
    }
  };
}