package net.opentechnology.triki.mtd.pages;

public class MtdVatLiabilities extends MtdVatManage {

  public MtdVatLiabilities() {
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("liabilities");
  }

}
