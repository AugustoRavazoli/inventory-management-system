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
    editItem(index, price) {
      const item = this.items[index];
      item.price = price;
      if (item.quantity === "") item.quantity = 1;
    },
    removeItem(index) {
      this.items.length > 1 && this.items.splice(index, 1);
    },
    get totalPrice() {
      return this.items
        .map(item => item.amount)
        .reduce((prev, next) => prev + next);
    }
  };
}