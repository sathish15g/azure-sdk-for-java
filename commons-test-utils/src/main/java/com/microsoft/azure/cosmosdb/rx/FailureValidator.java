/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.Error;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.WFConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface FailureValidator {

    static FailureValidator.Builder builder() {
        return new Builder();
    }

    void validate(Throwable t);

    class Builder {
        private List<FailureValidator> validators = new ArrayList<>();

        public FailureValidator build() {
            return new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    for (FailureValidator validator : validators) {
                        validator.validate(t);
                    }
                }
            };
        }

        public <T extends Throwable> Builder statusCode(int statusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getStatusCode()).isEqualTo(statusCode);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsnGreaterThan(long quorumAckedLSN) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(BridgeInternal.getLSN((DocumentClientException) t) > quorumAckedLSN).isTrue();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsnGreaterThanEqualsTo(long quorumAckedLSN) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(BridgeInternal.getLSN((DocumentClientException) t) >= quorumAckedLSN).isTrue();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder exceptionQuorumAckedLSNInNotNull() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException documentClientException = (DocumentClientException) t;
                    long exceptionQuorumAckedLSN = -1;
                    if (documentClientException.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN) != null) {
                        exceptionQuorumAckedLSN = Long.parseLong((String) documentClientException.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN));

                    }
                    assertThat(exceptionQuorumAckedLSN).isNotEqualTo(-1);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder errorMessageContains(String errorMsg) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getMessage()).contains(errorMsg);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder errorMessageContain(int statusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getStatusCode()).isEqualTo(statusCode);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder notNullActivityId() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getActivityId()).isNotNull();
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder error(Error error) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getError().toJson()).isEqualTo(error.toJson());
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder subStatusCode(Integer substatusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getSubStatusCode()).isEqualTo(substatusCode);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder nullSubStatusCode() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getSubStatusCode()).isNull();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder responseHeader(String key, String value) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getResponseHeaders().get(key)).isEqualTo(value);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsn(long lsn) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException ex = (DocumentClientException) t;
                    assertThat(BridgeInternal.getLSN(ex)).isEqualTo(lsn);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder partitionKeyRangeId(String pkrid) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException ex = (DocumentClientException) t;
                    assertThat(BridgeInternal.getPartitionKeyRangeId(ex)).isEqualTo(pkrid);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceAddress(String resourceAddress) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException ex = (DocumentClientException) t;
                    assertThat(BridgeInternal.getResourceAddress(ex)).isEqualTo(resourceAddress);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder instanceOf(Class<T> cls) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(cls);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder sameAs(Exception exception) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isSameAs(exception);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder resourceNotFound() {
            
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException ex = (DocumentClientException) t;
                    assertThat(ex.getStatusCode()).isEqualTo(404);
                    
                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceTokenNotFound() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(IllegalArgumentException.class);
                    IllegalArgumentException ex = (IllegalArgumentException) t;
                    assertThat(ex.getMessage()).isEqualTo(RMResources.ResourceTokenNotFound);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder resourceAlreadyExists() {
            
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException ex = (DocumentClientException) t;
                    assertThat(ex.getStatusCode()).isEqualTo(409);
                    
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder causeInstanceOf(Class<T> cls) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t.getCause()).isNotNull();
                    assertThat(t.getCause()).isInstanceOf(cls);
                }
            });
            return this;
        }
    }
}
