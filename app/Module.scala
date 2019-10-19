import com.google.inject.AbstractModule
import ipcress.services.DigesterService

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[DigesterService]).asEagerSingleton()
  }

}
