package net.opentechnology.triki.mtd.pages;

public class MtdVatReturns extends MtdVatManage {

  public MtdVatReturns() {
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("returns");
  }
}
