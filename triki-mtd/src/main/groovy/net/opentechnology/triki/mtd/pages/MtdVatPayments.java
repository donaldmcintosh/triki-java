package net.opentechnology.triki.mtd.pages;

public class MtdVatPayments extends MtdVatManage {

  public MtdVatPayments() {
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("payments");
  }
}
