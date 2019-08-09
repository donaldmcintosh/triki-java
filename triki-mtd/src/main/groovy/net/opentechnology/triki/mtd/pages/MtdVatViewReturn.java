package net.opentechnology.triki.mtd.pages;

import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.mtd.security.HmrcIdentityProvider;
import net.opentechnology.triki.mtd.vatapi.client.HmrcClientUtils;
import net.opentechnology.triki.mtd.vatapi.client.HmrcVatClient;
import net.opentechnology.triki.mtd.vatapi.dto.VatObligations;
import net.opentechnology.triki.mtd.vatapi.dto.VatReturn;
import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import retrofit2.Call;
import retrofit2.Response;

public class MtdVatViewReturn extends MtdVatManage {

  private static final Logger logger = Logger.getLogger(MtdVatObligations.class);
  public static final String PERIOD_KEY = "periodKey";

  @SpringBean
  private SessionUtils sessionUtils;

  @SpringBean
  private HmrcClientUtils hmrcClientUtils;

  private FeedbackStringContainer viewReturnFeedback;

  private final String periodKey;

  public MtdVatViewReturn(PageParameters parameters) {
    super(parameters);
    periodKey = parameters.get(PERIOD_KEY).toString();
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("returns");

    // Validation
    String accessToken = sessionUtils.getModuleToken(HmrcIdentityProvider.HMRC_TOKEN);
    String vrn = getVrn();

    try {
      HmrcVatClient hmrcVatClient = hmrcClientUtils.buildAuthBearerServiceClient(accessToken);
      Call<VatReturn> callable = hmrcVatClient.viewReturn(vrn, periodKey);

      Response<VatReturn> vatReturnResponse = callable.execute();

      if(vatReturnResponse.code() == 200) {
        // Populate fields/labels
      }

    } catch (Exception e) {
      logger.error("Problems calling HMRC", e);
      viewReturnFeedback.setMsg(getResourceBundleMsg("problems"));
    }
  }
}
